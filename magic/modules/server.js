var fabric = require('fabric').fabric;
var express = require("express")
var app = express()
var _ = require("lodash");
var fs = require("fs");

import Table from "./table";
import EventFactory from "./eventFactory";
import CardService from "./cardService";
import Card from "./Card";

app.use(express.static('dist/client'))
app.use(express.static('public'))

var WebSocketServer = require('ws').Server;
var wss = new WebSocketServer({ port: 8080 });

var connections = [];
function emit(e,exclude) {
    _.each(connections,function(conn) {
        if (!_.includes(exclude,conn)) conn.send(JSON.stringify(e));
    });
}

var canvas = fabric.createCanvasForNode(200, 200);
var table = new Table(canvas,emit);

var cardNameCache = {};
fs.readFile("vintage.txt","utf8",function(err,list) {
    var names = list.split("\n");
    names = _.take(names,5);
    console.log("names",names);
    _.each(names,function(name) {
        console.log("Adding Card: ",name);
        CardService.findCard(name).then(function(card) {
            console.log("card from meta",card.id);
            cardNameCache[name] = card.id;
            Card.fromMetadata(card,function(o) {
                canvas.add(o);
                table.regenerateObjectsById();
            }.bind(this));
        }.bind(this));
    }.bind(this));
});
table.processEvent(EventFactory.spawnBooster(300,300,"KLD"));

wss.on('connection', function(ws) {
    connections.push(ws);
    ws.on('message', function(message) {
        var e = JSON.parse(message);
        if (e.broadcast !== false) emit(e,[ws]);
        table.processEvent(e);
    });

    ws.on("close",function() {
        _.remove(connections,(o) => o === ws);
    });
});

app.get("/state", function (req, res) {
    var serialized = table.canvas.toJSON(["id"]);
    res.send(serialized)
})

app.listen(3000, function () {
    console.log("Example app listening on port 3000!")
})
