package joe.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.jetbrains.annotations.Async;

public class AsyncStuff {

  private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
  private final Thread worker;

  private final ExecutorService executorService = Executors.newFixedThreadPool(2);

  public AsyncStuff() {
    worker =
        new Thread(
            () -> {
              try {
                while (true) {
                  queue.take().run();
                }
              } catch (InterruptedException e) {
                // just stop
              }
            });
    worker.start();
  }

  private void scheduleCustom(@Async.Schedule Integer input, CompletableFuture<Integer> result)
      throws InterruptedException {
    Runnable task = () -> executeCustom(input, result);
    System.out.println("Scheduling custom: " + input);
    queue.put(task);
  }

  private void executeCustom(@Async.Execute Integer input, CompletableFuture<Integer> result) {
    result.complete(methodToDebug(input));
  }

  private static int methodToDebug(Integer input) {
    // Set a breakpoint in this method
    // Expect to see an async stack trace for both executor and custom cases
    System.out.println("Processing " + input);
    return input;
  }

  int roundTripViaExecutor(int input) throws InterruptedException, ExecutionException {
    System.out.println("Scheduling through executor: " + input);
    return executorService.submit(() -> methodToDebug(input)).get();
  }

  int roundTrip(int input) throws InterruptedException, ExecutionException {
    CompletableFuture<Integer> result = new CompletableFuture<>();
    scheduleCustom(input, result);
    return result.get();
  }

  void close() {
    worker.interrupt();
    executorService.shutdown();
  }
}
