//https://docs.magicthegathering.io/#get-a-specific-set
function guid() {
    return Math.random().toString(36).substring(2, 15) +
        Math.random().toString(36).substring(2, 15);
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

function spawnCard(canvas,card,cb) {
    fabric.Image.fromURL(card.imageUrl, function(oImg) {
        oImg.hasControls = false;
        oImg.lockRotation = true;
        oImg.lockScalingX = true;
        oImg.lockScalingY = true;
        canvas.add(oImg);
        if (cb) cb(oImg);
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
                o.id = guid();
                order[o.id] = index;
                restack(canvas,order);
            })
        });
    });

    /*
    var panning = false;
    canvas.on('mouse:up', function (e) {
        panning = false;
    });

    canvas.on('mouse:down', function (e) {
        if (e.target == null) panning = true;
    });
    canvas.on('mouse:move', function (e) {
        if (panning && e && e.e) {
            var units = 10;
            var delta = new fabric.Point(e.e.movementX, e.e.movementY);
            canvas.relativePan(delta);
        }
    });

    canvas.on("mousewheel",function(e) {
        console.log(e);
    });
    */
});
