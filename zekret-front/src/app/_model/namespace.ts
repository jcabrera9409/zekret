import { CredentialResponseDTO } from "./credential";

export class NamespaceResponseDTO {
    name: string;
    zrn: string;
    description: string;
    createdAt: Date;
    updatedAt: Date;
    credentials: CredentialResponseDTO[];
}

export class NamespaceRequestDTO {
    name: string;
    description: string;
}