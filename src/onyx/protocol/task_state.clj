(ns onyx.protocol.task-state)

(defprotocol PTaskStateMachine
  (killed? [this])
  (start [this])
  (stop [this scheduler-event])
  (new-iteration? [this])
  (advanced? [this])
  (next-replica! [this replica])
  (next-cycle! [this])
  (get-input-pipeline [this])
  (get-output-pipeline [this])
  (set-replica! [this new-replica])
  (set-sealed! [this new-sealed?])
  (sealed? [this])
  (get-replica [this])
  (get-windows-state [this])
  (set-windows-state! [this new-windows-state])
  (get-lifecycle [this])
  (initial-sync-backoff [this])
  (log-state [this])
  (heartbeat! [this])
  (reset-event! [this])
  (set-event! [this new-event])
  (evict-peer! [this peer-id])
  (update-event! [this f])
  (get-event [this])
  (set-messenger! [this new-messenger])
  (get-messenger [this])
  (set-coordinator! [this new-coordinator])
  (get-coordinator [this])
  (set-context! [this new-context])
  (get-context [this])
  (replica-version [this])
  (epoch [this])
  (set-epoch! [this epoch])
  (next-epoch! [this])
  (goto-recover! [this])
  (goto-next-batch! [this])
  (goto-next-iteration! [this])
  (min-epoch [this])
  (set-min-epoch! [this min-epoch])
  (exec [this])
  (advance [this]))
