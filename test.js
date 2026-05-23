fetch('http://localhost:8080/api/reparti/carica')
  .then(res => res.json())
  .then(data => console.log(data))
  .catch(e => console.error(e));
