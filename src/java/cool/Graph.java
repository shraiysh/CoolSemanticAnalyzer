package cool;

import java.util.*; 
  
class Graph<T> { 
  
  // We use Hashmap to store the edges in the graph 
  private Map<T, List<T> > map = new HashMap<>();

  // This function adds a new vertex to the graph 
  public void addVertex(T s) 
  { 
    map.put(s, new LinkedList<T>()); 
  } 

  // This function adds the edge 
  // between source to destination 
  public void addEdge(T source, 
                      T destination, 
                      boolean bidirectional) 
  { 

    if (!map.containsKey(source)) 
      addVertex(source); 

    if (!map.containsKey(destination)) 
      addVertex(destination); 

    map.get(source).add(destination); 
    if (bidirectional == true) { 
      map.get(destination).add(source); 
    } 
  } 

  // This function gives the count of vertices 
  public int getVertexCount() 
  { 
    return map.keySet().size();
  } 

  // This function gives the count of edges 
  public int getEdgesCount(boolean bidirection) 
  { 
    int count = 0; 
    for (T v : map.keySet()) { 
      count += map.get(v).size(); 
    } 
    if (bidirection == true) { 
      count = count / 2; 
    } 
    return count;
  } 

  // This function gives whether 
  // a vertex is present or not. 
  public boolean hasVertex(T s) 
  { 
    return map.containsKey(s);
  } 

  // This function gives whether an edge is present or not. 
  public boolean hasEdge(T s, T d) 
  { 
    return hasVertex(s) && map.get(s).contains(d);
  }

  // Utility function for recursively checking cycles in graph
  private boolean cycleUtil(T i, Map<T, Boolean> visited, Map<T, Boolean> recStack) {
    // Mark the current node as visited and 
    // part of recursion stack 
    if (recStack.containsKey(i) && recStack.get(i)) 
      return true; 

    if (visited.containsKey(i) && visited.get(i)) 
      return false; 
        
    visited.put(i, true); 

    recStack.put(i, true);
    List<T> children = map.get(i); 
      
    for (T c: children) {
      if(cycleUtil(c, visited, recStack)) {
        return true;
      }
    }
          
    recStack.put(i, false); 
    return false;
  }

  // Checks for presence of cycles in graph
  public boolean isCyclic() {
    Map<T, Boolean> visited = new HashMap<T, Boolean>();
    Map<T, Boolean> recStack = new HashMap<T, Boolean>();
    for(Map.Entry<T, List<T>> entry: map.entrySet()) {
      if(cycleUtil(entry.getKey(), visited, recStack)) {
        return true;
      }
    }
    return false;
  }

  // Prints the adjancency list of each vertex. 
  public String toString(String connector) 
  { 
    StringBuilder builder = new StringBuilder(); 

    for (T v : map.keySet()) { 
      builder.append(v.toString() + connector); 
      for (T w : map.get(v)) { 
        builder.append(w.toString() + " "); 
      } 
      builder.append("\n"); 
    } 

    return (builder.toString()); 
  }
} 