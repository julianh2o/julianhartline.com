//https://docs.magicthegathering.io/#get-a-specific-set
function guid() {
    return Math.random().toString(36).substring(2, 15) +
        Math.random().toString(36).substring(2, 15);
}

function getAbsoluteCenterPoint(o) {
  var point = o.getCenterPoint();
  if (!o.group)
    return point;
  var groupPoint = getAbsoluteCenterPoint(o.group);
  return {
    x: point.x + groupPoint.x,
    y: point.y + groupPoint.y
  };
}

function containsInGroupPoint(o,point) {
  if (!o.group)
    return o.containsPoint(point);

  var center = getAbsoluteCenterPoint(o);
  var thisPos = {
      xStart: center.x - o.width/2,
      xEnd: center.x + o.width/2,
      yStart: center.y - o.height/2,
      yEnd: center.y + o.height/2
  }

  if (point.x >= thisPos.xStart && point.x <= (thisPos.xEnd)) {
      if (point.y >= thisPos.yStart && point.y <= thisPos.yEnd) {
          return true;
      }
  }
  return false;
}

function findCard(name,cb) {
    $.get("https://api.magicthegathering.io/v1/cards?name="+name,function(result) {
        var cards = result.cards;
        var firstCard = _.filter(cards,"imageUrl")[0];
        if (cb) cb(firstCard);
    });
}

function generateBooster(set,cb) {
    $.get("https://api.magicthegathering.io/v1/sets/"+set+"/booster",function(result) {
        var cards = result.cards;
        if (cb) cb(cards);
    });
}

function setControls(o) {
    o.hasControls = false;
    o.lockRotation = true;
    o.lockScalingX = true;
    o.lockScalingY = true;
}

function spawnCard(canvas,card,cb) {
    fabric.Image.fromURL(card.imageUrl, function(o) {
        setControls(o);
        o.kind = "card";
        o.card = card;
        o.id = guid();
        canvas.add(o);
        if (cb) cb(o);
    });
}

var order = {};
function restack(canvas,order) {
    var arr = _(order).toPairs().sortBy(1).value();
    var objById = {};
    _.each(canvas.getObjects(),function(o) {
        objById[o.id] = o;
    });

    _.each(arr,function(v,index) {
        objById[v[0]].moveTo(index);
    });

}

function objectAt(canvas,x,y,exclude) {
    var point = new fabric.Point(x,y);
    if (!$.isArray(exclude)) exclude = [exclude];
    var found = null;
    _.each(canvas.getObjects(),function(o) {
        if (!_.includes(exclude,o) && o.containsPoint(point)) {
            found = o;
        }
    });
    return found;
}

function positionCollectionCards(ox,oy,objects,kind) {
    if (kind == "collection") {
        _.each(objects,function(o,index) {
            o.set({left: ox, top: oy + index*35});
            o.setCoords();
        });
    }
}

function disbandGroup(canvas,group) {
    var objects = group._objects;
    group._restoreObjectsState();
    canvas.remove(group);
    _.each(objects,function(o) {
        canvas.add(o);
    });
    return objects;
}

function createCollection(canvas,objects,kind) {
    objects = _.flatten(_.map(objects,function(o) {
        if (o.type === "group") {
            return disbandGroup(canvas,o);
        } else {
            return o;
        }
    }));
    var origin = objects[0];
    positionCollectionCards(origin.left, origin.top, objects, "collection");

    var group = new fabric.Group(objects);
    group.id = guid();
    group.kind = "collection";
    setControls(group);
    _.each(objects,function(o) {
        canvas.remove(o);
    });
    canvas.add(group);
    return group;
}

function addToCollection(canvas,group,o) {
    var n = group.getObjects().length;
    var groupKind = group.kind;
    var groupId = group.id;

    var objects = group._objects;
    group._restoreObjectsState();
    canvas.remove(group);
    _.each(objects,function(o) {
        canvas.add(o);
    });

    if (o != null) {
        if (o.kind == "collection") {
            var objects = o._objects;
            o._restoreObjectsState();
            canvas.remove(o);
            _.each(objects,function(o) {
                canvas.add(o);
                objects.push(o);
            });
        } else {
            objects.push(o);
        }
    }

    var group = createCollection(canvas,objects,groupKind);
    group.id = groupId;
}

$(document).ready(function() {
    var canvas = new fabric.Canvas("c");
    canvas.selection = false;

    $(window).resize(function() {
        canvas.setWidth(window.innerWidth);
        canvas.setHeight(window.innerHeight);
    });
    $(window).resize();

    generateBooster("KLD",function(cards) {
        _.each(cards,function(card,index) {
            spawnCard(canvas,card,function(o) {
                o.set({left: 10, top: index*35});
                o.setCoords();
                order[o.id] = index;
                restack(canvas,order);
            })
        });
    });

    var panning = false;
    canvas.on('mouse:up', function (e) {
        if (!panning) {
            var obj = objectAt(canvas,e.e.clientX,e.e.clientY,e.target);
            if (obj) {
                if (obj.kind == "card") {
                    var group = createCollection(canvas,[obj,e.target],"stack");
                    console.log("created group",group.id);
                } else {
                    console.log("adding to group",obj.id);
                    addToCollection(canvas,obj,e.target);
                }
            }
        }
        panning = false;
    });

    canvas.on('mouse:beforedrag', function (e) {
        var target = canvas.findTarget(e);
        if (e.shiftKey && target.kind == "collection") {
            var group = target;
            var found = null;
            _.each(group.getObjects(),function(o) {
                if (containsInGroupPoint(o,new fabric.Point(e.clientX,e.clientY))) {
                    found = o;
                }
            });
            if (found) {
                console.log("removing from group",found.id,found.card.name);
                var left = found.left;
                var top = found.top;
                group.removeWithUpdate(found);
                canvas.add(found);
                if (group._objects.length == 1) {
                    var o = group._objects[0];
                    group._restoreObjectsState();
                    canvas.remove(group);
                    canvas.add(o);
                } else {
                    addToCollection(canvas,group,null);
                }
                found.bringToFront();
                found.setCoords();
            }
        }
    });

    canvas.on('mouse:down', function (e) {
        console.log("mouse down called",e.target == null ? null : e.target.kind);
        if (e.target == null) panning = true;
    });
    var laste = null;
    canvas.on('mouse:move', function (e) {
        laste = e;
        /*
        if (panning && e && e.e) {
            var units = 10;
            var delta = new fabric.Point(e.e.movementX, e.e.movementY);
            canvas.relativePan(delta);
        }
        */
    });

    var last = null;
    var zoom = 1;
    $(window).on("mousewheel",function(e) {
        if (last && new Date().getTime() - last < 100) return;
        //e.originalEvent.deltaX,e.originalEvent.deltaY
        if (e.originalEvent.deltaY > 0) {
            zoom = zoom * 1.2;
        } else if (e.originalEvent.deltaY < 0) {
            zoom = zoom / 1.2;
        }
        canvas.setZoom(zoom)
        last = new Date().getTime();
    });

    $(window).on("keydown",function(e) {
        //console.log(e.keyCode);
    });
});
