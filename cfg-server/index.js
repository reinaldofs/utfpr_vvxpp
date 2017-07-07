const path = require("path")
const { promisify } = require("util")
const child_process = require("child_process")
const express = require("express")
const fs = require("fs-extra")

const exec = promisify(child_process.exec)
const dir = path.join(`${__dirname}/bytecode-to-cfg-master`)

const app = express()
app.use("/public", express.static(`${dir}/html/`))

app.get("/:className/:methodName/*", function(req, res) {
  const path = req.params["0"]
  const { className, methodName } = req.params

  fs
    .copy(
      `${path}/${className}.class`,
      `${dir}/target/classes/fr/univnantes/bytecodetocfg/${className}.class`
    )
    .then(() =>
      exec(
        `cd ${dir} && mvn compile && mvn exec:java -Dexec.args="${className} ${methodName}"`
      )
    )
    .then(() => res.sendFile(`${dir}/html/graph.html`))
    .catch(err => res.send(err))
})

app.listen(8888)
