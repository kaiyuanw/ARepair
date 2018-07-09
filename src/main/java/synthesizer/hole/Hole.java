package synthesizer.hole;

import generator.fragment.Fragment;
import java.util.List;

public abstract class Hole {

  protected Fragment value;
  protected boolean updated;
  protected List<? extends Fragment> fragments;
  /**
   * If the hole is active, then we can print the fragment value of the hole.  Otherwise, we ignore
   * the hole and print the node the hole is mapped just like the hole does not exist.
   */
  protected boolean isActive;

  public Hole() {
    this.value = null;
    this.updated = false;
    this.fragments = null;
    this.isActive = true;
  }

  @Override
  public abstract String toString();

  public void setValue(Fragment value) {
    updated = true;
    this.value = value;
  }

  public Fragment getValue() {
    return value;
  }

  public boolean isUpdated() {
    return updated;
  }

  public List<? extends Fragment> getFragments() {
    return fragments;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }
}
