(function() {

    var frm = document.getElementById('convert'),
        out = document.getElementById('out');

    frm.addEventListener("submit", function(event) {
        event.preventDefault();
        convert(this);
    });

    function convert(frm) {
        var xhr = new XMLHttpRequest();
        xhr.addEventListener("load", function(event) {
            showMsg(event.target.responseText);
        });
        xhr.addEventListener("error", function(event) {
            showMsg('Oups! Something goes wrong.');
        });
        xhr.open("GET", frm.action + frm.val.value + '?figures=' + frm.fig.value);
        xhr.setRequestHeader("Accept", "application/json");
        xhr.send();
        showMsg('...');
    }

    function showMsg(msg) {
        if (out.innerText !== undefined) {
            out.innerText = msg;
        } else {
            out.textContent = msg;
        }
    }

})();
