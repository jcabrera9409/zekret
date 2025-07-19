import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { ErrorService } from '../_service/error.service';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const errorService = inject(ErrorService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      errorService.handleError(error);
      return throwError(() => error);
    })
  )
};
