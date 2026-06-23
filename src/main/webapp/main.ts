(globalThis as any).global = globalThis;
import('./bootstrap').catch((err: unknown) => console.error(err)); // NO SONAR
