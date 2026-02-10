import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('auth_token');

  // Public GET-only endpoints (no auth needed for reading)
  const publicGetEndpoints = [
    '/api/v3/book',
    '/api/v3/comment',
    '/api/v3/rating'
  ];

  // Always public endpoints (any method)
  const alwaysPublicEndpoints = [
    '/api/v3/auth/signin',
    '/api/v3/auth/signup'
  ];

  // Extract pathname from URL
  const getPathname = (url: string): string => {
    try {
      return new URL(url).pathname;
    } catch {
      // Fallback for relative URLs
      return url.split('?')[0];
    }
  };

  const pathname = getPathname(req.url);

  // Check if this is a GET request to a public endpoint
  const isPublicGet = req.method === 'GET' &&
    publicGetEndpoints.some(endpoint => pathname.startsWith(endpoint));

  const isAlwaysPublic = alwaysPublicEndpoints.some(endpoint =>
    pathname.includes(endpoint)
  );

  // Only add token if NOT a public endpoint
  if (token && !isPublicGet && !isAlwaysPublic && !isTokenExpired(token)) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // Clear expired tokens
  if (token && isTokenExpired(token)) {
    console.log('Token expired, clearing storage');
    localStorage.removeItem('auth_token');
    localStorage.removeItem('username');
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('username');

        const currentUrl = router.url;
        if (!currentUrl.includes('/login') && !currentUrl.includes('/signup')) {
          router.navigate(['/login']);
        }
      }
      return throwError(() => error);
    })
  );
};

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp * 1000;
    return exp <= Date.now();
  } catch (error) {
    console.error('Failed to parse token', error);
    return true;
  }
}
