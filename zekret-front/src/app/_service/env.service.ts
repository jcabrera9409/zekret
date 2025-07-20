import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvService {

  private production: boolean;
  private apiUrl: string;
  private tokenName: string;
  private domains: string[];
  private disallowedRoutes: string[];

  constructor() { 
    const env = (window as any).__env || {};

    this.production = env.production || false;
    this.apiUrl = env.apiUrl || 'http://localhost:8080/v1';
    this.tokenName = env.token_name || 'access_token';
    this.domains = env.domains || ['localhost:8080'];
    this.disallowedRoutes = env.disallowedRoutes || [
      'http://localhost:8080/v1/auth/login', 
      'http://localhost:8080/v1/users/register'
    ];
  }

  isProduction(): boolean {
    return this.production;
  }

  getApiUrl(): string {
    return this.apiUrl;
  }

  getTokenName(): string {
    return this.tokenName;
  }

  getDomains(): string[] {
    return this.domains;
  }

  getDisallowedRoutes(): string[] {
    return this.disallowedRoutes;
  }
}
