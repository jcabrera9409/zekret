import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';

import { JwtModule } from '@auth0/angular-jwt';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { errorInterceptor } from './interceptors/error.interceptor';

// Token getter function para JWT usando localStorage directamente
export function jwtTokenGetter(): string {
  return localStorage.getItem('access_token') || '';
}

export const appConfig: ApplicationConfig = {
  providers: [
    importProvidersFrom(
      JwtModule.forRoot({
        config: {
          tokenGetter: jwtTokenGetter,
          allowedDomains: ['localhost:8080'],
          disallowedRoutes: [
            'http://localhost:8080/v1/auth/login',
            'http://localhost:8080/v1/users/register'
          ]
        }
      })
    ),
    provideHttpClient(
      withInterceptorsFromDi(),
      withInterceptors([errorInterceptor])
    ),
    provideRouter(routes), 
    provideAnimationsAsync()
  ]
};
