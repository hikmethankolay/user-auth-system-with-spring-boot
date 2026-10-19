# Kubernetes manifests

A hardened starting point for running the image on Kubernetes with Kustomize: two or more
replicas, probes on the management port, a read-only root filesystem, a non-root user, an
autoscaler, a disruption budget and a network policy. PostgreSQL, Redis and SMTP are expected to
exist already (managed services or their own charts).

1. Edit `configmap.yaml` (hosts, URLs, origins) and the image tag in `kustomization.yaml`.
2. Create the secrets (never commit them):

   ```bash
   kubectl create namespace backend
   kubectl -n backend create secret generic app-secrets \
     --from-literal=DB_USERNAME=app --from-literal=DB_PASSWORD='...' \
     --from-literal=JWT_SECRET="$(openssl rand -base64 48)" \
     --from-literal=REDIS_PASSWORD='...' \
     --from-literal=SPRING_MAIL_USERNAME='...' --from-literal=SPRING_MAIL_PASSWORD='...'
   ```

3. Apply: `kubectl apply -k deploy/kubernetes`
4. Route an Ingress or Gateway to the `app` Service (port 80) with TLS.

Scrape `/actuator/prometheus` on port 8081 (the pods carry `prometheus.io/*` annotations) and load
`ops/prometheus/alerts.yml` into Prometheus.
