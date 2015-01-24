var express = require('express')
var bodyParser = require('body-parser');
var app = express()

app.use(bodyParser.urlencoded({ extended: true }));

app.use(function(req, res, next){
  start = Date.now()
  next();
  finished = Date.now()

  console.log(req.method + " in " + (finished - start))
});

app.get('/res/:id', function (req, res) {
  var id = req.params.id.split("_"); 
  if (id.length != 2) {
    res.status(404).end();
  }

  console.log("GET /res/" + req.params.id + ", time to finish: " + ((parseInt(id[0]) - Date.now())/1000))
  
  if (parseInt(id[0]) > Date.now()) {
    res.send("PROCESSING");
  } else {
    res.send("FINISHED");
  }
})

function one_or_two_min() {
  return (Math.floor(Math.random() * 100)%3 + 1)*1000*60;
}

app.post('/res', function (req, res) {
  id = "" + (Date.now() + one_or_two_min()) + "_" + Math.floor(Math.random() * 1000000)
  console.log("POST /res: " + id)
  
  res.status(201).send(id)
})

var port = process.env.PORT | 9999;
var server = app.listen(port, function () {
  var host = server.address().address
  var port = server.address().port

  console.log('Example app listening at http://%s:%s', host, port)
})
