import { inject } from "@angular/core";
import { Router, RouterStateSnapshot } from "@angular/router";
import { Observable } from "rxjs";
import { AuthService } from "./auth.service";
import { UtilMethods } from "../util/util";

export const authGuard = (state: RouterStateSnapshot): Observable<boolean> | boolean => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.estaLogueado()) {
        authService.cerrarSesion();
        return false;
    }
    else {
        if (!UtilMethods.isTokenExpired()) {
            let url: String = state.url;
            const username = UtilMethods.getUsernameFieldJwtToken();
            return true;
        } else {
            authService.cerrarSesion();
            return false;
        }
    }

}