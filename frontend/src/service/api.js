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

export async function getUsers() {
  const response = await fetch(makeUrl("/users"));
  if (response.status === 200) {
    const users = response.json();
    return users;
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
  const response = await fetch(makeUrl("/users"), {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(newUser),
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