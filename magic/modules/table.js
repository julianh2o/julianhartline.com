import Util from "./util";

var fabric = require('fabric').fabric;
var _ = require("lodash");
var fetch = require('node-fetch');

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
    fetch("https://api.magicthegathering.io/v1/cards?name="+name).then((res) => res.json()).then(function(result) {
        var cards = result.cards;
        var firstCard = _.filter(cards,"imageUrl")[0];
        if (cb) cb(firstCard);
    });
}

function generateBooster(set,cb) {
    fetch("https://api.magicthegathering.io/v1/sets/"+set+"/booster").then((res) => res.json()).then(function(result) {
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
        o.id = Util.guid();
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
    group.id = Util.guid();
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

class Table {
    constructor(canvas) {
        this.canvas = canvas;
        canvas.selection = false;

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
    }
}

export default Table;

