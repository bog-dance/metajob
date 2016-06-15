def getJobs() {
    def hi = hudson.model.Hudson.instance
    def jobs = hi.getItems(hudson.model.Job)
    def vuJobs = []
    for (String job : jobs) {
        def m = "${job.displayName}" =~ "prod-vu" 
        if (m) {
            vuJobs.add("${job.displayName}")
        }
    }
    return vuJobs
}

int threads = 10

def vuJobs = getJobs()
def branches = [:]
def stack = []
int jobsCount = vuJobs.size()
int groups = jobsCount.intdiv(threads) + 1

        println 'vuJobs: ' + vuJobs
        println '\n\n\n'
        println 'jobsCount: ' + jobsCount
        println 'threads: ' + threads
        println 'groups: ' + groups

for (j = 1; j <= groups; j++) {
    for (i = 1; i <= threads; i++) {
        try { 
            stack.add(vuJobs.first())
            vuJobs.remove(0)
            } 
        catch (e) {
            }
                       }

    println 'Group' + "${j}" + ': ' + stack                   

    for (String sjob : stack) {
            def ssjob = "${sjob}"
            branches['g'+"${j}"+'.'+"${sjob}"] = {
                build job: "${ssjob}" ,
                propagate: false,
                parameters: [
                    [$class: 'BooleanParameterValue', name: 'SWITCH_VERSION', value: false], 
                    [$class: 'StringParameterValue',  name: 'VERSION', value: 'v1.0.0'], 
                    [$class: 'BooleanParameterValue', name: 'DEPLOY_GIT_COMMIT', value: false], 
                    [$class: 'StringParameterValue',  name: 'GIT_COMMIT', value: '*/master'],
                    [$class: 'BooleanParameterValue', name: 'CREATE_DB', value: false], 
                    [$class: 'BooleanParameterValue', name: 'INSTALL_WP_CONFIG', value: false], 
                    [$class: 'BooleanParameterValue', name: 'INSTALL_NGINX_CONFIG', value: false],
                    [$class: 'BooleanParameterValue', name: 'UPLOAD_DB_DUMP', value: false] 
                        ]            
                } 
        }

    stack.clear()
        node {
            stage 'deploy on vultrs'
            parallel branches
            }
    branches.clear()
    }
