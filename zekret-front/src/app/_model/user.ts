export class UserResponseDTO {
    email: string;
    username: string;
    password: string;
    createdAt: string;
    enabled: boolean;
}

export class UserRequestDTO {
    email: string;
    username: string
    password: string;
}