export const environment = {
    production: false,
    apiUrl: 'http://localhost:8080/v1',
    token_name: 'access_token',
    domains: ['localhost:8080'],
    disallowedRoutes: ['http://localhost:8080/v1/auth/login', 'http://localhost:8080/v1/users/register']
};
