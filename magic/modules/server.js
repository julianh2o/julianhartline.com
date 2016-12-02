var fabric = require('fabric').fabric;
var express = require("express")
var app = express()

import Table from "./table";

app.use(express.static('dist/client'))

var canvas = fabric.createCanvasForNode(200, 200);
var table = new Table(canvas);

app.get("/state", function (req, res) {
    var serialized = JSON.stringify(table.canvas);
    res.send(serialized)
})

app.listen(3000, function () {
    console.log("Example app listening on port 3000!")
})
