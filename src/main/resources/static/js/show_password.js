function showPassword() {

    const x = document.getElementById("password");
    const y = document.getElementById("passwordConfirm");

    if (x.type === "password" || y.type === "password") {
        x.type = "text";
        y.type = "text";
    } else {
        x.type = "password";
        y.type = "password";
    }
}