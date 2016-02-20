[![Build Status](https://snap-ci.com/ind9/gocd-mesos/branch/master/build_image)](https://snap-ci.com/ind9/gocd-mesos/branch/master)

GoCD Mesos
==========
> Autoscale GO agents with mesos!

GoCD Mesos is a mesos framework, that can autoscale go agents on top of a mesos cluster based on the job backlog in the GoCD server. GoCD Mesos uses the GoCD API to calculate the backlog and figure out the agent demand.

How it works:
-------------
1. The framework starts a poller which polls the GoCD Server every two minutes to find out if there are any pending jobs.
2. If there are pending jobs for more than 5 continous attempts, the framework launches a GoCD agent using a docker container.
3. Currently, the GoCD backlog calculation is very primitive. Here is a list of things which the framework DOESN'T DO.
    - Downscaling agents when the demands are satisfied.
    - Differentiating between backlog caused by agent demand vs backlog caused by other errors.
    - Matching the demand at once by launching multiple go agents. 
4. This is very early, pre alpha version of the software WHICH IS NOT SUPPOSED TO BE USED IN PRODUCTION.


How to run:
-----------
```bash
$ git clone https://github.com/ind9/gocd-mesos.git
$ cd gocd-mesos && mvn clean package
$ java -cp target/gocd-mesos-0.1.jar com.indix.mesos.GoCDMesosFramework path/to/framework.conf
```

NOTE 1: If you are running the framework in a remote machine, instead of the machine/network where mesos master is running - please set the LIBPROCESS_IP=<public_accessible_ip_of_remote_machine> in your env. This is to make sure the framework binds on that public address instead of localhost, so that the mesos master can talk to it back. 

NOTE 2: The framework doesn't assume any user id on mesos slaves. So the mesos slaves should be running with --switch_user=false option. Otherwise the tasks will be launched with same users as framework is running and it MAY cause problems. 

Configuration:
--------------
```
gocd-mesos {
  # Mesos master URL. Should be accessible from where the framework is running. The mesos master should
  # also able to be talk to framework. 
  mesos-master: "mesos-master:5050"

  # GO server config to poll and connect the agent
  go-server {
    host: "localhost"
    port: "8080"
    user-name: "random-guy"
    password: "random-insecure-string"
  }

  go-agent {
    # This key should be already shared with the go server and configured in its config.xml as autoRegisterKey
    auto-register-key: "random_UUID_key"
    # Docker image containing the go agent. The below is image is tested to work with the framework. But you are free to
    # use any image with gocd-agent.
    docker-image: "travix/gocd-agent:latest"
  }
}
```

Requirements
------------

1. Scala (2.11+)
2. Mesos (0.20+)
3. GoCD (15.2+)


Things to fix:
=============
- [x] Write tests!
- [x] Fix the poller to poll only in larger fixed durations instead of polling every 5 seconds. Also, Bake in exponential backoff if there are nothing to be scheduled.(Fix: made the poller, poll only every two minutes. Not sure about exponential backoff - we require it or not)
- [x] Make the agent to auto enable by default. Right now, after registration it stucks at pending state.
- [ ] Run and verify against a go server behind authentication.
- [ ] Allow graceful downscaling of agents.
- [ ] Evaluate mesos HTTP scheduler API
