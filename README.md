GOCD Mesos
==========


> Autoscale GO agents with mesos!


Requirements
------------

1. Scala (2.11+)
2. Mesos (0.20+)


Things to fix:
=============

- [x] Fix the poller to poll only in larger fixed durations instead of polling every 5 seconds. Also, Bake in exponential backoff if there are nothing to be scheduled.(Fix: made the poller, poll only every two minutes. Not sure about exponential backoff - we require it or not)
- [x] Make the agent to auto enable by default. Right now, after registration it stucks at pending state.
- [ ] Run and verify against a go server behind authentication.
- [ ] Evaluate mesos HTTP scheduler API
