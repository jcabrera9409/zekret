import { Credential } from "./credential";

export class Namespace {
    name: string;
    zrn: string;
    description: string;
    createdAt: Date;
    updatedAt: Date;
    credentials: Credential[];
}