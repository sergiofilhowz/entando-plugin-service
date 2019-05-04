# Entando Project

## Install

You'll need Minikube and MySQL locally.

### Create a kubernetes namespace

```
$ kubectl create namespace entando
```

### Configure env variables
>- `DB_HOST`: Default `localhost`
>- `DB_PORT`: Default `3306`
>- `DB_NAME`: Default `entando_plugin`
>- `DB_USER`: Default `root`
>- `DB_PASS`: Default `root`