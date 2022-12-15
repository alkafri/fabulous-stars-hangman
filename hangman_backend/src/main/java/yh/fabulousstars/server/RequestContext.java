package yh.fabulousstars.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public record RequestContext(
        String endpoint,
        String session,
        HttpServletRequest req,
        HttpServletResponse resp) {
}
