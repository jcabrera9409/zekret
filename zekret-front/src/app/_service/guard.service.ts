import { inject } from "@angular/core";
import { Observable } from "rxjs";
import { AuthService } from "./auth.service";
import { UtilMethods } from "../util/util";

export const authGuard = (): Observable<boolean> | boolean => {
    const authService = inject(AuthService);
    const utilMethods = inject(UtilMethods);

    if (!authService.isLogged()) {
        authService.logout().subscribe({
            next: () => sessionStorage.clear(),
            error: () => sessionStorage.clear()
        });
        return false;
    }
    else {
        if (!utilMethods.isTokenExpired()) {
            return true;
        } else {
            authService.logout().subscribe({
                next: () => sessionStorage.clear(),
                error: () => sessionStorage.clear()
            });
            return false;
        }
    }

}