package joe.async;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.*;

public class AsyncStuff implements AutoCloseable {

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

  @Override
  public void close() {
    worker.interrupt();
    executorService.shutdown();
  }

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    try (var a = new AsyncStuff()) {
      a.roundTrip(3);
      a.roundTripViaExecutor(6);
    }
  }
}

final class Async {

  /**
   * Indicates that the marked method schedules async computation.
   * Scheduled object is either {@code this}, or the annotated parameter value.
   */
  @Retention(RetentionPolicy.CLASS)
  @Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
  public @interface Schedule {}

  /**
   * Indicates that the marked method executes async computation.
   * Executed object is either {@code this}, or the annotated parameter value.
   * This object needs to match with the one annotated with {@link org.jetbrains.annotations.Async.Schedule}
   */
  @Retention(RetentionPolicy.CLASS)
  @Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
  public @interface Execute {}
}

