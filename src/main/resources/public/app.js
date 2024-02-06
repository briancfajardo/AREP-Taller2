document.getElementById('movieForm').addEventListener('submit', function(event) {
    event.preventDefault();
    loadGetMsg();
});
function loadGetMsg() {
    let nameVar = document.getElementById("name").value;
    const xhttp = new XMLHttpRequest();
    xhttp.onload = function() {
        document.getElementById("getrespmsg").innerHTML = this.responseText;
    };
    xhttp.open("GET", "/movie?name="+nameVar);
    xhttp.send();
}