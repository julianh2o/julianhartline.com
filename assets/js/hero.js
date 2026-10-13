// Homepage hero: a sparse hexagon field growing in from the right edge of the content column.
// Pulses of light travel along the hexagon edges, and panels light up in a
// fading rainbow.
(function () {
  var canvas = document.querySelector(".hero-art");
  if (!canvas || !canvas.getContext) return;

  var ctx = canvas.getContext("2d");
  var reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  var SQRT3 = Math.sqrt(3);
  var NEIGHBORS = [[1, 0], [1, -1], [0, -1], [-1, 0], [-1, 1], [0, 1]];

  var width = 0, height = 0, radius = 26;
  var cells = [], verts = [], edgePath = null;
  var pulses = [];
  var running = false, visible = true, last = 0, nextSpawn = 0;

  function rand(min, max) { return min + Math.random() * (max - min); }

  // Rainbow hue that drifts over time and across the field.
  function hueAt(x, time) { return (time * 0.02 + x * 0.22) % 360; }

  function build() {
    var rect = canvas.getBoundingClientRect();
    var dpr = window.devicePixelRatio || 1;
    width = rect.width;
    height = rect.height;
    canvas.width = Math.round(width * dpr);
    canvas.height = Math.round(height * dpr);
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    radius = width < 600 ? 20 : 26;

    // Candidate cells covering the canvas (pointy-top axial coordinates).
    var grid = {};
    var rows = Math.ceil(height / (radius * 1.5)) + 1;
    for (var r = -1; r <= rows; r++) {
      var qMin = Math.floor(-2 / SQRT3 - r / 2) - 1;
      var qMax = Math.ceil((width + 2 * radius) / (radius * SQRT3) - r / 2);
      for (var q = qMin; q <= qMax; q++) {
        grid[q + "," + r] = {
          q: q, r: r,
          x: radius * SQRT3 * (q + r / 2),
          y: radius * 1.5 * r
        };
      }
    }

    // Grow a contiguous, sparse patch from the right edge. Each candidate is
    // tried once, with a chance of joining that falls off toward the left.
    var included = {}, tried = {}, frontier = [];
    function chance(x) {
      var t = x / width;
      return t <= 0 ? 0 : Math.min(0.92, Math.pow(t, 0.9) + 0.15);
    }
    Object.keys(grid).forEach(function (key) {
      var c = grid[key];
      if (c.x > width - radius * 2.5 && Math.random() < 0.9) {
        included[key] = c;
        tried[key] = true;
        frontier.push(c);
      }
    });
    while (frontier.length) {
      var cell = frontier.splice(Math.floor(Math.random() * frontier.length), 1)[0];
      NEIGHBORS.forEach(function (d) {
        var key = (cell.q + d[0]) + "," + (cell.r + d[1]);
        var n = grid[key];
        if (!n || tried[key]) return;
        tried[key] = true;
        if (Math.random() < chance(n.x)) {
          included[key] = n;
          frontier.push(n);
        }
      });
    }

    // Shared vertices and deduplicated edges.
    var vertIndex = {}, edgeSeen = {};
    cells = [];
    verts = [];
    edgePath = new Path2D();
    function vertexId(x, y) {
      var key = Math.round(x * 10) + ":" + Math.round(y * 10);
      if (!(key in vertIndex)) {
        vertIndex[key] = verts.length;
        verts.push({ x: x, y: y, adj: [], cells: [] });
      }
      return vertIndex[key];
    }
    Object.keys(included).forEach(function (key) {
      var c = included[key];
      var path = new Path2D();
      var ids = [];
      for (var i = 0; i < 6; i++) {
        var a = Math.PI / 180 * (60 * i - 30);
        var vx = c.x + radius * Math.cos(a);
        var vy = c.y + radius * Math.sin(a);
        ids.push(vertexId(vx, vy));
        if (i === 0) path.moveTo(vx, vy); else path.lineTo(vx, vy);
      }
      path.closePath();
      var cellObj = { x: c.x, y: c.y, path: path, glow: 0, hue: 0 };
      cells.push(cellObj);
      for (var j = 0; j < 6; j++) {
        var v1 = ids[j], v2 = ids[(j + 1) % 6];
        verts[v1].cells.push(cellObj);
        var ek = Math.min(v1, v2) + "-" + Math.max(v1, v2);
        if (edgeSeen[ek]) continue;
        edgeSeen[ek] = true;
        verts[v1].adj.push(v2);
        verts[v2].adj.push(v1);
        edgePath.moveTo(verts[v1].x, verts[v1].y);
        edgePath.lineTo(verts[v2].x, verts[v2].y);
      }
    });
    pulses = [];
  }

  function spawnPulse(time) {
    if (!verts.length || pulses.length >= 22) return;
    var start = Math.floor(Math.random() * verts.length);
    var v = verts[start];
    if (!v.adj.length) return;
    pulses.push({
      path: [start],
      to: v.adj[Math.floor(Math.random() * v.adj.length)],
      t: 0,
      speed: rand(80, 150),
      hops: Math.floor(rand(6, 18)),
      hue: hueAt(v.x, time)
    });
  }

  function lightCell(cell, amount, time) {
    if (cell.glow < amount) {
      cell.glow = amount;
      cell.hue = hueAt(cell.x, time);
    }
  }

  function advance(p, dt, time) {
    var from = verts[p.path[p.path.length - 1]];
    var to = verts[p.to];
    var len = Math.hypot(to.x - from.x, to.y - from.y);
    p.t += (p.speed * dt) / len;
    while (p.t >= 1) {
      p.t -= 1;
      var prev = p.path[p.path.length - 1];
      p.path.push(p.to);
      if (p.path.length > 6) p.path.shift();
      verts[p.to].cells.forEach(function (c) { lightCell(c, 0.4, time); });
      var options = verts[p.to].adj.filter(function (n) { return n !== prev; });
      if (--p.hops <= 0 || !options.length) return false;
      p.to = options[Math.floor(Math.random() * options.length)];
    }
    return true;
  }

  function drawPulse(p) {
    var from = verts[p.path[p.path.length - 1]];
    var to = verts[p.to];
    var head = { x: from.x + (to.x - from.x) * p.t, y: from.y + (to.y - from.y) * p.t };

    // Trail back along the visited vertices, fading out over ~70px.
    var trail = 70, travelled = 0, prev = head;
    for (var i = p.path.length - 1; i >= 0 && travelled < trail; i--) {
      var v = verts[p.path[i]];
      var seg = Math.hypot(prev.x - v.x, prev.y - v.y);
      var end = v;
      if (travelled + seg > trail) {
        var k = (trail - travelled) / seg;
        end = { x: prev.x + (v.x - prev.x) * k, y: prev.y + (v.y - prev.y) * k };
      }
      var a0 = 1 - travelled / trail;
      travelled = Math.min(trail, travelled + seg);
      var a1 = 1 - travelled / trail;
      var grad = ctx.createLinearGradient(prev.x, prev.y, end.x, end.y);
      grad.addColorStop(0, "hsla(" + p.hue + ",100%,70%," + a0 + ")");
      grad.addColorStop(1, "hsla(" + p.hue + ",100%,70%," + a1 + ")");
      ctx.strokeStyle = grad;
      ctx.lineWidth = 2.5;
      ctx.beginPath();
      ctx.moveTo(prev.x, prev.y);
      ctx.lineTo(end.x, end.y);
      ctx.stroke();
      prev = end;
    }

    var glow = ctx.createRadialGradient(head.x, head.y, 0, head.x, head.y, 10);
    glow.addColorStop(0, "hsla(" + p.hue + ",100%,85%,1)");
    glow.addColorStop(1, "hsla(" + p.hue + ",100%,60%,0)");
    ctx.fillStyle = glow;
    ctx.beginPath();
    ctx.arc(head.x, head.y, 10, 0, Math.PI * 2);
    ctx.fill();
  }

  function draw() {
    ctx.clearRect(0, 0, width, height);
    for (var i = 0; i < cells.length; i++) {
      var c = cells[i];
      ctx.fillStyle = "rgba(255,255,255,0.025)";
      ctx.fill(c.path);
      if (c.glow > 0.01) {
        ctx.fillStyle = "hsla(" + c.hue + ",90%,60%," + (c.glow * 0.55) + ")";
        ctx.fill(c.path);
      }
    }
    ctx.strokeStyle = "rgba(255,255,255,0.09)";
    ctx.lineWidth = 1;
    ctx.stroke(edgePath);

    ctx.globalCompositeOperation = "lighter";
    pulses.forEach(drawPulse);
    ctx.globalCompositeOperation = "source-over";
  }

  function frame(time) {
    if (!running) return;
    var dt = Math.min(0.05, (time - (last || time)) / 1000);
    last = time;

    var decay = Math.exp(-dt / 1.4);
    cells.forEach(function (c) { c.glow *= decay; });
    if (cells.length && Math.random() < dt * 5) {
      lightCell(cells[Math.floor(Math.random() * cells.length)], 1, time);
    }
    if (time >= nextSpawn) {
      spawnPulse(time);
      nextSpawn = time + rand(200, 550);
    }
    pulses = pulses.filter(function (p) { return advance(p, dt, time); });

    draw();
    requestAnimationFrame(frame);
  }

  function start() {
    if (running || reduceMotion || !visible) return;
    running = true;
    last = 0;
    requestAnimationFrame(frame);
  }

  function drawStill() {
    cells.forEach(function (c) {
      if (Math.random() < 0.18) { c.glow = rand(0.3, 0.8); c.hue = hueAt(c.x, 0); }
    });
    draw();
  }

  build();
  if (reduceMotion) drawStill(); else start();

  var lastWidth = width, resizeTimer = null;
  window.addEventListener("resize", function () {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(function () {
      var w = canvas.getBoundingClientRect().width;
      var h = canvas.getBoundingClientRect().height;
      if (w === lastWidth && h === height) return;
      lastWidth = w;
      build();
      if (reduceMotion) drawStill();
    }, 150);
  });

  // Stop animating while the hero is scrolled out of view.
  if ("IntersectionObserver" in window) {
    new IntersectionObserver(function (entries) {
      visible = entries[0].isIntersecting;
      if (visible) start(); else running = false;
    }).observe(canvas);
  }
})();
