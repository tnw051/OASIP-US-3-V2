const baseUrl = import.meta.env.PROD ? import.meta.env.VITE_API_URL : "/api";

function makeUrl(path) {
  return `${baseUrl}${path}`;
}

//GET
export async function getEvents() {
  const response = await fetch(makeUrl("/events"));
  if (response.status === 200) {
    const events = response.json();
    console.log(events);
    return events;
  } else {
    console.log("Cannot fetch events");
  }
}

export async function getCategories() {
  const response = await fetch(makeUrl("/categories"));
  if (response.status === 200) {
    const categories = response.json();
    return categories;
  } else {
    console.log("Cannot fetch events");
  }
}

//CREATE
export async function createEvent(newEvent) {
  const response = await fetch(makeUrl("/events"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(newEvent),
  });

  const data = await response.json();
  if (response.status === 201) {
    return data;
  } else if (response.status === 400) {
    throw data;
  } else {
    console.log("Cannot create event");
  }
}

//DELETE
export async function deleteEvent(id) {
  const response = await fetch(makeUrl(`/events/${id}`), {
    method: "DELETE",
  });

  if (response.status === 200) {
    return true;
  } else {
    console.log("Cannot delete event");
    return false;
  }
}

//UPDATE
export async function updateEvent(id, editEvent) {
  const response = await fetch(makeUrl(`/events/${id}`), {
    method: "PATCH",
    headers: {
      "content-type": "application/json",
    },
    body: JSON.stringify(editEvent),
  });
  if (response.status === 200) {
    const updatedEvent = await response.json();
    return updatedEvent;
  } else {
    console.log("Cannot edit event");
  }
}

export async function getEventsByCategoryIdOnDate(categoryId, startAt) {
  const response = await fetch(
    makeUrl(`/events?categoryId=${categoryId}&startAt=${startAt}`)
  );
  if (response.status === 200) {
    const events = response.json();
    return events;
  } else {
    console.log("Cannot fetch events");
  }
}

export async function getEventsByCategoryId(categoryId) {
  const response = await fetch(makeUrl(`/events?categoryId=${categoryId}`));
  if (response.status === 200) {
    const events = response.json();
    return events;
  } else {
    console.log("Cannot fetch events");
  }
}

export async function getEventsByFilter(filter) {
  const { categoryId, type, startAt } = filter;

  let uri = "/events?";
  const filters = [];

  if (categoryId) {
    filters.push(`categoryId=${categoryId}`);
  }

  if (type) {
    filters.push(`type=${type}`);
  }

  if (startAt) {
    filters.push(`startAt=${startAt}`);
  }

  if (filters.length > 0) {
    uri += filters.join("&");
  }

  const response = await fetch(makeUrl(uri));
  if (response.status === 200) {
    const events = response.json();
    return events;
  } else {
    console.log("Cannot fetch events");
  }
}

export async function updateCategory(id, editCategory) {
  const response = await fetch(makeUrl(`/categories/${id}`), {
    method: "PATCH",
    headers: {
      "content-type": "application/json",
    },
    body: JSON.stringify(editCategory),
  });
  if (response.status === 200) {
    const updatedCategory = await response.json();
    return updatedCategory;
  } else {
    console.log("Cannot edit category");
  }
}

export async function getUsers(options = {}) {
  const {onUnauthorized} = options;
  const response = await fetch(makeUrl("/users"), {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    }
  });
  if (response.status === 200) {
    const users = response.json();
    return users;
  } else if (response.status === 401) {
    onUnauthorized();
  } else {
    console.log("Cannot fetch users");
  }
}

export async function getRoles() {
  const response = await fetch(makeUrl("/users/roles"));
  if (response.status === 200) {
    const users = response.json();
    return users;
  } else {
    console.log("Cannot fetch user roles");
  }
}

export async function createUser(newUser) {
  const trimmedUser = {
    ...newUser,
    name: newUser.name.trim(),
    email: newUser.email.trim(),
  };

  const response = await fetch(makeUrl("/users"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(trimmedUser),
  });

  const data = await response.json();
  if (response.status === 201) {
    return data;
  } else if (response.status === 400) {
    throw data;
  } else {
    console.log("Cannot create user");
  }
}

export async function deleteUser(id) {
  const response = await fetch(makeUrl(`/users/${id}`), {
    method: "DELETE",
  });

  if (response.status === 204) {
    return true;
  } else {
    console.log("Cannot delete user");
    return false;
  }
}

export async function updateUser(id, changes) {
  const response = await fetch(makeUrl(`/users/${id}`), {
    method: "PATCH",
    headers: {
      "content-type": "application/json",
    },
    body: JSON.stringify(changes),
  });
  if (response.status === 200) {
    const updatedUser = await response.json();
    return updatedUser;
  } else {
    console.log("Cannot edit user");
  }
}

// {
//   "timestamp": "2022-08-29T15:25:36.839+00:00",
//   "status": 401,
//   "error": "Unauthorized",
//   "path": "/api/auth/match",
//   "message": "Password NOT Matched"
// }

// @PostMapping("/match")
// public String match(@Valid @RequestBody MatchRequest matchRequest) {
//     try {
//         boolean matches = service.match(matchRequest);
//         if (!matches) {
//             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Password NOT Matched");
//         }
//         return "Password Matched";
//     } catch (EntityNotFoundException e) {
//         throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
//     }
// }

export async function match(matchRequest) {
  const response = await fetch(makeUrl("/auth/match"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(matchRequest),
  });

  if (response.status === 200) {
    return true;
  } else if (response.status === 401) {
    return false;
  } else if (response.status === 404) {
    const data = await response.json();
    throw new Error(data.message);
  } else {
    console.log("Cannot match password");
  }
}

/**
 * @typedef {Object} LoginResponse
 * @property {string} token
 * @property {string} type
 */
/**
 * @param {Object} loginRequest
 * @param {Object} options
 * @param {Function} options.onSuccess
 * @param {Function} options.onUnauthorized
 * @param {Function} options.onNotFound
 * @returns {Promise<void>}
 */
export async function login(loginRequest, options = {}) {
  const { onSuccess, onUnauthorized, onNotFound } = options;
  const response = await fetch(makeUrl("/auth/login"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(loginRequest),
  });

  const data = await response.json();
  if (response.status === 200) {
    onSuccess(data);
  } else if (response.status === 401) {
    onUnauthorized(data);
  } else if (response.status === 404) {
    onNotFound(data);
  } else {
    console.log("Cannot login");
  }
}