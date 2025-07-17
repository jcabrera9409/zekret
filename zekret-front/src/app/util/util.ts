import { JwtHelperService } from "@auth0/angular-jwt";
import { environment } from "../../environments/environment.development";

export class UtilMethods {
    public static getJwtToken(): string {
        let token = localStorage.getItem(environment.token_name);
        return token;
    }

    public static getUsernameFieldJwtToken(): string {
        const username = this.getFieldJwtToken('username');
        return username ? username : '';
    }

    public static getFieldJwtToken(field: string): string {
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

    private static getDecodedJwtToken(): any {
        let token = this.getJwtToken();
        return this.extractJwtPayload(token);
    }

     private static extractJwtPayload(token: string): any {
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

    public static getHelper(): JwtHelperService {
        return new JwtHelperService();
    }

    public static setJwtToken(token: string): void {
        localStorage.setItem(environment.token_name, token);
    }

    public static removeJwtToken(): void {
        localStorage.removeItem(environment.token_name);
    }
}