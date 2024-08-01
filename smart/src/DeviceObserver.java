public class DeviceObserver implements Observer {
    @Override
    public void update(String deviceId, String status) {
        System.out.println("DeviceObserver: Device " + deviceId + " status updated to: " + status);
    }
}
