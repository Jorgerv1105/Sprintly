import { goto } from "$app/navigation";
import axios from "axios";

export function configurarAxios() {
    const token = localStorage.getItem("token");
    if (token) {
        axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    }
}

export function protegerRuta() {
    const token = localStorage.getItem("token");
    if (!token) {
        goto("/login");
        return;
    }
    configurarAxios();
}

export function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("rol");
    localStorage.removeItem("nombre");
    axios.defaults.headers.common["Authorization"] = "";
    goto("/login");
}

export function activarLogoutAutomatico() {
    document.addEventListener("visibilitychange", () => {
        if (document.hidden) {
            logout();
        }
    });
}