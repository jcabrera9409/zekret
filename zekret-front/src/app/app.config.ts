import { ApplicationConfig, importProvidersFrom, inject } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { HttpInterceptorFn, provideHttpClient, withInterceptors } from '@angular/common/http';
import { errorInterceptor } from './interceptors/error.interceptor';
import { UtilMethods } from './util/util';

const authInterceptor: HttpInterceptorFn = (req, next) => {
  const utilMethods = inject(UtilMethods);
  const url = req.url;  
  // URLs que NO deben tener Authorization header
  const authFreeRoutes = [
    '/v1/auth/login',
    '/v1/users/register'
  ];
  
  const shouldSkipAuth = authFreeRoutes.some(route => url.includes(route));
  
  if (shouldSkipAuth) {
    return next(req);
  }
  
  const token = utilMethods.getJwtToken();
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  
  return next(req);
};

export const appConfig: ApplicationConfig = {
  providers: [
    UtilMethods,
    provideHttpClient(
      withInterceptors([authInterceptor, errorInterceptor])
    ),
    provideRouter(routes), 
    provideAnimationsAsync()
  ]
};
