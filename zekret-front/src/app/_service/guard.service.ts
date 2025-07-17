import { inject } from "@angular/core";
import { Observable } from "rxjs";
import { AuthService } from "./auth.service";
import { UtilMethods } from "../util/util";

export const authGuard = (): Observable<boolean> | boolean => {
    const authService = inject(AuthService);

    if (!authService.isLogged()) {
        authService.logout();
        return false;
    }
    else {
        if (!UtilMethods.isTokenExpired()) {
            return true;
        } else {
            authService.logout();
            return false;
        }
    }

}