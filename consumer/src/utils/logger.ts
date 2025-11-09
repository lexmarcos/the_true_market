import pino from 'pino';
import path from 'path';
import { config } from '../config/config';
import fs from 'fs';

// Ensure logs directory exists
if (!fs.existsSync(config.logging.dir)) {
  fs.mkdirSync(config.logging.dir, { recursive: true });
}

/**
 * Create logger instance with file and console transports
 */
export const logger = pino({
  level: config.logging.level,
  serializers: {
    error: pino.stdSerializers.err,
  },
  transport: {
    targets: [
      {
        target: 'pino-pretty',
        level: config.logging.level,
        options: {
          colorize: true,
          translateTime: 'SYS:standard',
          ignore: 'pid,hostname',
        },
      },
      {
        target: 'pino/file',
        level: config.logging.level,
        options: {
          destination: path.join(config.logging.dir, 'app.log'),
          mkdir: true,
        },
      },
    ],
  },
});

/**
 * Create child logger for specific component
 */
export function createLogger(component: string) {
  return logger.child({ component });
}

export default logger;
