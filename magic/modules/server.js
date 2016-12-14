var fabric = require('fabric').fabric;
var express = require("express")
var app = express()
var _ = require("lodash");

import Table from "./table";

app.use(express.static('dist/client'))

var canvas = fabric.createCanvasForNode(200, 200);
var table = new Table(canvas);
table.generateBooster("KLD");

app.get("/state", function (req, res) {
    var serialized = table.canvas.toJSON(["id"]);
    res.send(serialized)
})

app.listen(3000, function () {
    console.log("Example app listening on port 3000!")
})

var WebSocketServer = require('ws').Server;
var wss = new WebSocketServer({ port: 8080 });

var connections = [];
wss.on('connection', function(ws) {
    connections.push(ws);
    ws.on('message', function(message) {
        var e = JSON.parse(message);
        _.each(connections,(conn) => conn === ws ? null : conn.send(JSON.stringify(e)));
        table.processEvent(e);
    });

    ws.on("close",function() {
        _.remove(connections,(o) => o === ws);
    });
});
