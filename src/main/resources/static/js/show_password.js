function showPassword() {

    const x = document.getElementById("password");

    if (x.type === "password") {
    x.type = "text";
    } else {
        x.type = "password";
    }
}