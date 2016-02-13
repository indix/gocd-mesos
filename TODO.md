Things to fix:
=============

[] Fix the poller to poll only in larger fixed durations instead of polling every 5 seconds. Also, Bake in exponential backoff if there are nothing to be scheduled.
[] Make the agent to auto enable by default. Right now, after registration it stucks at pending state.
[] Run and verify against a go server behind authentication.
