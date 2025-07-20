import { Injectable } from "@angular/core";
import { JwtHelperService } from "@auth0/angular-jwt";
import { EnvService } from "../_service/env.service";

@Injectable({
    providedIn: 'root'
})
export class UtilMethods {

    constructor(private envService: EnvService) {}

    public getJwtToken(): string {
        let token = localStorage.getItem(this.envService.getTokenName());
        return token;
    }

    public getUsernameFieldJwtToken(): string {
        const username = this.getFieldJwtToken('username');
        return username ? username : '';
    }

    public getFieldJwtToken(field: string): string {
        const decodeToken = this.getDecodedJwtToken();
        if (decodeToken) {
            if (field in decodeToken) {
                return decodeToken[field];
            }
            return "";
        }
        else {
            return null;
        }
    }

    public isTokenExpired(): boolean {
        const helper = this.getHelper();
        let token = this.getJwtToken();

        return helper.isTokenExpired(token);
    }

    private getDecodedJwtToken(): any {
        let token = this.getJwtToken();
        return this.extractJwtPayload(token);
    }

     private extractJwtPayload(token: string): any {
        if (!token) {
            return null;
        }

        const parts = token.split('.');
        if (parts.length !== 3) {
            return null;
        }

        try {
            const helper = this.getHelper();
            const decodedToken = helper.decodeToken(token);

            return decodedToken;
        } catch (e) {
            return null;
        }
    }

    public getHelper(): JwtHelperService {
        return new JwtHelperService();
    }

    public setJwtToken(token: string): void {
        localStorage.setItem(this.envService.getTokenName(), token);
    }

    public removeJwtToken(): void {
        localStorage.removeItem(this.envService.getTokenName());
    }

    // Métodos estáticos para compatibilidad hacia atrás (opcional)
    public static getInstance(): UtilMethods {
        // Este método requiere que se inyecte el servicio donde se use
        throw new Error('Use dependency injection instead of static methods');
    }
}