FROM node:24-alpine AS deps
WORKDIR /app

RUN apk update && apk upgrade && apk add --no-cache libc6-compat

COPY package.json package-lock.json* ./
RUN npm ci

FROM node:24-alpine AS builder
WORKDIR /app

RUN apk update && apk upgrade && apk add --no-cache libc6-compat

COPY --from=deps /app/node_modules ./node_modules
COPY . .

RUN npm run build

FROM node:24-alpine AS runner
WORKDIR /app

RUN apk update && apk upgrade --no-cache

ENV NODE_ENV=production

RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 nextjs

COPY --from=builder /app/package.json ./package.json

COPY --from=builder --chown=nextjs:nodejs /app/.next ./.next
COPY --from=builder --chown=nextjs:nodejs /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/node_modules ./node_modules

USER nextjs

EXPOSE 3000

ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

CMD ["npm", "start"]