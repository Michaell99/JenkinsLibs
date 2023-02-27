This pipelines were made by Michael

What you will need to make it work:

1. AWS cli
link https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html
2. cloudbees plugin for aws credentials for this pipeline.
link https://plugins.jenkins.io/aws-credentials/
3.  make a general bucket for backups this pipeline will help you out and make the right folders so many teams can use the same bucket
4. make a temp bucket for temp backups in order to roll back safely

please note that the node in the pipeline could be any node just make sure it contains aws cli 