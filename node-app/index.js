var express = require('express')
var bodyParser = require('body-parser');
var app = express()

app.use(bodyParser.urlencoded({ extended: true }));

app.get('/res/:id', function (req, res) {
  var id = req.params.id.split("_"); 
  if (id.length != 2) {
    res.status(404).end();
  }

  if (parseInt(id[0]) < Date.now()) {
    res.send("PROCESSING");
  } else {
    res.send("FINISHED");
  }
})

app.post('/res', function (req, res) {
  id = Date.now() + "_" + Math.floor(Math.random() * 1000000)
  res.status(201).send(id)
})

var port = process.env.PORT | 9999;
var server = app.listen(port, function () {
  var host = server.address().address
  var port = server.address().port

  console.log('Example app listening at http://%s:%s', host, port)
})
