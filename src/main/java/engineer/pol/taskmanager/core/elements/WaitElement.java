package engineer.pol.taskmanager.core.elements;

import java.time.Duration;

public class WaitElement extends TaskElement {

    private Duration duration;

    public WaitElement(Duration duration) {
        super(ElementType.WAIT);
        this.duration = duration;
    }

    public long getTime() {
        return this.duration.toMillis();
    }

    public Duration getDuration() {
        return duration;
    }

    public void setTime(Duration duration) {
        this.duration = duration;
    }

}
