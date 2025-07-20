(function (window) {
  window.__env = window.__env || {};

  // Estos valores serán reemplazados en tiempo de deploy
  window.__env.production = false;
  window.__env.apiUrl = 'http://localhost:8080/v1';
  window.__env.token_name = 'access_token';
  window.__env.domains = ['localhost:8080'];
  window.__env.disallowedRoutes = [
    'http://localhost:8080/v1/auth/login', 
    'http://localhost:8080/v1/users/register'
  ];
})(this);