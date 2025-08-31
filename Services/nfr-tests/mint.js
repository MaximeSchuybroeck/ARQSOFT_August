const fs = require("fs");
const jwt = require("jsonwebtoken");
const key = fs.readFileSync(process.argv[2], "utf8");
const now = Math.floor(Date.now()/1000);
const claims = {
  iss: "example.io",
  sub: "1,perf-tester@example.com",
  roles: "ROLE_ADMIN"
};
const token = jwt.sign(claims, key, { algorithm: "RS256", expiresIn: 3600 });
console.log(token);
