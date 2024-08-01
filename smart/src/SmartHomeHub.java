import java.util.*;
import java.util.function.Consumer;

public class SmartHomeHub implements Subject {
    private Map<String, Device> devices = new HashMap<>();
    private List<ScheduledTask> scheduledTasks = new ArrayList<>();
    private TriggerManager triggerManager = new TriggerManager();
    private Map<String, Consumer<Scanner>> commandMap = new HashMap<>();
    private boolean running = true;
    private List<Observer> observers = new ArrayList<>();

    public SmartHomeHub() {
        commandMap.put("add", this::addCommand);
        commandMap.put("remove", this::removeCommand);
        commandMap.put("turnOn", this::turnOnCommand);
        commandMap.put("turnOff", this::turnOffCommand);
        commandMap.put("schedule", this::scheduleCommand);
        commandMap.put("trigger", this::triggerCommand);
        commandMap.put("status", this::statusCommand);
        commandMap.put("tasks", this::tasksCommand);
        commandMap.put("triggers", this::triggersCommand);
        commandMap.put("exit", this::exitCommand);
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String deviceId, String status) {
        for (Observer observer : observers) {
            observer.update(deviceId, status);
        }
    }

    public void addDevice(Device device) {
        DeviceProxy proxy = new DeviceProxy(device);
        devices.put(device.getId(), proxy);
        notifyObservers(device.getId(), device.getStatus());
    }

    public void removeDevice(String id) {
        devices.remove(id);
        notifyObservers(id, "removed");
    }

    public void turnOnDevice(String id) {
        Device device = devices.get(id);
        if (device != null) {
            device.turnOn();
            notifyObservers(id, device.getStatus());
        }
    }

    public void turnOffDevice(String id) {
        Device device = devices.get(id);
        if (device != null) {
            device.turnOff();
            notifyObservers(id, device.getStatus());
        }
    }

    public void setSchedule(String id, String time, String command) {
        Device device = devices.get(id);
        if (device != null) {
            scheduledTasks.add(new ScheduledTask(device, time, command));
            notifyObservers(id, "Scheduled task added");
        }
    }

    public void addTrigger(String condition, String action) {
        triggerManager.addTrigger(new Trigger(condition, action));
        notifyObservers("all", "Trigger added: " + condition + " -> " + action);
    }

    public void showStatus() {
        StringBuilder status = new StringBuilder();
        for (Device device : devices.values()) {
            status.append(device.getStatus()).append(". ");
        }
        System.out.println("Status Report: \"" + status.toString().trim() + "\"");
        notifyObservers("all", "Status report updated");
    }

    public void showScheduledTasks() {
        StringBuilder taskReport = new StringBuilder("[");
        for (ScheduledTask task : scheduledTasks) {
            taskReport.append("{device: ").append(task.getDevice().getId())
                      .append(", time: \"").append(task.getTime())
                      .append("\", command: \"").append(task.getCommand())
                      .append("\"}, ");
        }
        if (!scheduledTasks.isEmpty()) {
            taskReport.setLength(taskReport.length() - 2); // Remove trailing comma
        }
        taskReport.append("]");
        System.out.println("Scheduled Tasks: \"" + taskReport.toString() + "\"");
        notifyObservers("all", "Scheduled tasks updated");
    }

    public void showTriggers() {
        List<Trigger> triggers = triggerManager.getTriggers();
        StringBuilder triggerReport = new StringBuilder("[");
        for (Trigger trigger : triggers) {
            triggerReport.append("{condition: \"").append(trigger.getCondition())
                         .append("\", action: \"").append(trigger.getAction())
                         .append("\"}, ");
        }
        if (!triggers.isEmpty()) {
            triggerReport.setLength(triggerReport.length() - 2); // Remove trailing comma
        }
        triggerReport.append("]");
        System.out.println("Automated Triggers: \"" + triggerReport.toString() + "\"");
        notifyObservers("all", "Triggers updated");
    }

    // Command methods
    private void addCommand(Scanner scanner) {
        System.out.println("Enter device type (light, thermostat, doorlock):");
        String type = scanner.nextLine();
        System.out.println("Enter device ID:");
        String id = scanner.nextLine();
        if (type.equals("thermostat")) {
            System.out.println("Enter temperature:");
            int temp = Integer.parseInt(scanner.nextLine());
            addDevice(DeviceFactory.createDevice(type, id, temp));
        } else {
            addDevice(DeviceFactory.createDevice(type, id, 0));
        }
    }

    private void removeCommand(Scanner scanner) {
        System.out.println("Enter device ID to remove:");
        String removeId = scanner.nextLine();
        removeDevice(removeId);
    }

    private void turnOnCommand(Scanner scanner) {
        System.out.println("Enter device ID to turn on:");
        String turnOnId = scanner.nextLine();
        turnOnDevice(turnOnId);
    }

    private void turnOffCommand(Scanner scanner) {
        System.out.println("Enter device ID to turn off:");
        String turnOffId = scanner.nextLine();
        turnOffDevice(turnOffId);
    }

    private void scheduleCommand(Scanner scanner) {
        System.out.println("Enter device ID to schedule:");
        String scheduleId = scanner.nextLine();
        System.out.println("Enter time to schedule (HH:MM):");
        String time = scanner.nextLine();
        System.out.println("Enter command (Turn On, Turn Off):");
        String scheduleCommand = scanner.nextLine();
        setSchedule(scheduleId, time, scheduleCommand);
    }

    private void triggerCommand(Scanner scanner) {
        System.out.println("Enter condition for trigger (e.g., temperature > 75):");
        String condition = scanner.nextLine();
        System.out.println("Enter action for trigger (e.g., turnOff(1)):");
        String action = scanner.nextLine();
        addTrigger(condition, action);
    }

    private void statusCommand(Scanner scanner) {
        showStatus();
    }

    private void tasksCommand(Scanner scanner) {
        showScheduledTasks();
    }

    private void triggersCommand(Scanner scanner) {
        showTriggers();
    }

    private void exitCommand(Scanner scanner) {
        running = false;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SmartHomeHub hub = new SmartHomeHub();

        // Example Observer
        hub.addObserver(new DeviceObserver());

        while (hub.running) {
            System.out.println("Enter command (add, remove, turnOn, turnOff, schedule, trigger, status, tasks, triggers, exit):");
            String command = scanner.nextLine();

            Consumer<Scanner> action = hub.commandMap.get(command.split(" ")[0]);
            if (action != null) {
                action.accept(scanner);
            } else {
                System.out.println("Invalid command.");
            }
        }

        scanner.close();
    }
}
