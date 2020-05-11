package com.acunetix.model;

public enum ScanTaskState {
        Queued(0), Scanning(1), Archiving(2), Complete(3), Failed(4), Cancelled(5), Delayed(
                        6), Pausing(7), Paused(8), Resuming(9);

        private final int number;

        ScanTaskState(final int number) {
                this.number = number;
        }

        public int getNumber() {
                return number;
        }

        public String getNumberAsString() {
                return String.valueOf(number);
        }
}
