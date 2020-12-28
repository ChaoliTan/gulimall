pipeline {
  agent {
    node {
      label 'maven'
    }

  }

  parameters {
      string(name:'PROJECT_VERSION',defaultValue: 'v1.0',description:'project version')
      string(name:'PROJECT_NAME',defaultValue: 'gulimall-gateway',description:'init default project')
  }

  environment {
      DOCKER_CREDENTIAL_ID = 'dockerhub-id'
      GITHUB_CREDENTIAL_ID = 'github-id'
      KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
      REGISTRY = 'docker.io'
      DOCKERHUB_NAMESPACE = 'chaolitan'
      GITHUB_ACCOUNT = 'chaolitan'
      SONAR_CREDENTIAL_ID = 'sonar-token'
      BRANCH_NAME = 'main'
  }


  stages {
    stage('pull code') {
      steps {
        git(credentialsId: 'github-id', url: 'https://github.com/ChaoliTan/gulimall.git', branch: 'main', changelog: true, poll: false)
        sh 'echo $PROJECT_VERSION'
        sh 'echo $PROJECT_NAME'
        sh 'echo $GITHUB_CREDENTIAL_ID'
      }
    }

    stage ('unit test') {
        steps {
            container ('maven') {
                sh 'mvn clean install -Dmaven.test.skip=true'
            }
        }
    }

    stage('sonarqube analysis') {
      steps {
        container ('maven') {
          withCredentials([string(credentialsId: "$SONAR_CREDENTIAL_ID", variable: 'SONAR_TOKEN')]) {
            withSonarQubeEnv('sonar') {
             sh "mvn sonar:sonar -gs `pwd`/mvn-setting.xml -Dsonar.login=$SONAR_TOKEN"
            }
          }
          timeout(time: 1, unit: 'HOURS') {
            waitForQualityGate abortPipeline: true
          }
        }
      }
    }

    stage ('build & push') {
        steps {
            container ('maven') {
                sh 'mvn -Dmaven.test.skip=true -gs `pwd`/mvn-setting.xml clean package'
                sh 'cd $PROJECT_NAME && docker build --no-cache -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
                withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                    sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
                    sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
                }
            }
        }
    }

    stage('push latest'){
      when{
        branch 'main'
      }
      steps{
           container ('maven') {
             sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
             sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
           }
      }
    }

    stage('deploy to dev') {
      when{
        branch 'master'
      }
      steps {
        input(id: 'deploy-to-dev-$PROJECT_NAME', message: 'deploy $PROJECT_NAME to dev?')
        kubernetesDeploy(configs: 'deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
      }
    }

    stage('push with tag'){
      when{
        expression{
          return params.PROJECT_VERSION =~ /v.*/
        }
      }
      steps {
          container ('maven') {
            input(id: 'release-image-with-tag', message: 'release image with tag?')
              withCredentials([usernamePassword(credentialsId: "$GITHUB_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                sh 'git config --global user.email "ctan7749@gmail.com" '
                sh 'git config --global user.name "chaolitan" '
                sh 'git tag -a $PROJECT_VERSION -m "$PROJECT_VERSION" '
                sh 'git push http://$GIT_USERNAME:$GIT_PASSWORD@github.com/$GITHUB_ACCOUNT/gulimall.git --tags --ipv4'
              }
            sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
            sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
      }
      }
    }


  }
}